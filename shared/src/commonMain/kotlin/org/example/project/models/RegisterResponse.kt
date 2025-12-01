import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val shelterId: Long
)
