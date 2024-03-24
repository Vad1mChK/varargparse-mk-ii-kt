package org.vad1mchk.varargparse.mk2.config

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.vad1mchk.varargparse.mk2.exceptions.ConfigException
import java.io.File
import java.io.IOException

object Config {
    lateinit var privateConfig: PrivateConfig
    lateinit var publicConfig: PublicConfig

    private val configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.Property)

    fun loadPrivateConfigFromYaml(fileName: String) {
        privateConfig = loadYamlFromFile(fileName, PrivateConfig.serializer())
    }

    fun loadPublicConfigFromYaml(fileName: String) {
        publicConfig = loadYamlFromFile(fileName, PublicConfig.serializer())
    }

    @Throws(ConfigException::class)
    private inline fun <reified T> loadYamlFromFile(fileName: String, serializer: KSerializer<T>): T {
        try {
            val yamlString: String

            File(fileName).bufferedReader().use {
                yamlString = it.readText()
            }

            return Yaml.default.decodeFromString<T>(serializer, yamlString)
        } catch (e: Exception) {
            when (e) {
                is IOException -> {
                    throw ConfigException("Could not read config data from \"$fileName\" due to an I/O error.", e)
                }

                is SerializationException, is IllegalArgumentException -> {
                    throw ConfigException("Error when decoding the contents of the config file \"$fileName\".", e)
                }

                else -> {
                    throw e
                }
            }
        }
    }
}