package kalyanksd.online.satta.e.web_service.model

data class DepositModel(
    val user_id: String,
    val amount: String,
    val transaction_id: String,
    val payment_app: String
)
