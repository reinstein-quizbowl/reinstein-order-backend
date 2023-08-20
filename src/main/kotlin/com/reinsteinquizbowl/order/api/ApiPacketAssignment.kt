package com.reinsteinquizbowl.order.api

import java.time.LocalDate

data class ApiPacketAssignment(
    val type: String, // one of the Type constants
    val id: Long, // for the appropriate type
    val date: LocalDate, // to help us order the assignment of packets for non-conference games; conference games are assumed to be earlier than all non-conference games
    val sequence: Int,
    val description: String,
    val schoolIds: List<Long>,
    var packetId: Long? = null,
) {
    fun isMissingPacketAssignment() = packetId == null

    object Type {
        const val CONFERENCE = "conference"
        const val NON_CONFERENCE_GAME = "game"
    }
}
