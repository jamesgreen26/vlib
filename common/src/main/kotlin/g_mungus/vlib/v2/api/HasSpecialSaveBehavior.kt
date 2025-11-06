package g_mungus.vlib.v2.api

/** Implement with a BlockEntity to add custom actions when saved/loaded to a structure template**/
interface HasSpecialSaveBehavior {
    fun executeWhenSavingShip()
    fun executeAfterLoadingShip()
}