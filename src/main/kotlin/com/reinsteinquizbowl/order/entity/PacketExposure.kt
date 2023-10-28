package com.reinsteinquizbowl.order.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable

@Entity
@Immutable // view
data class PacketExposure(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null, // not necessarily consistent from query to query, but JPA needs some kind of ID
    val packetId: Long,
    val exposedSchoolId: Long,
    val source: String,
    val sourceId: Long,
    val bookingId: Long,
    val bookingCreationId: String,
    val ordererSchoolId: Long? = null,
    val tentativePacketExposure: Boolean,
    val confirmedPacketExposure: Boolean,
)
