package com.reinsteinquizbowl.order.service

import com.reinsteinquizbowl.order.api.ApiBooking
import com.reinsteinquizbowl.order.api.ApiBookingConference
import com.reinsteinquizbowl.order.api.ApiBookingPracticeCompilationOrder
import com.reinsteinquizbowl.order.api.ApiBookingPracticePacketOrder
import com.reinsteinquizbowl.order.api.ApiBookingPracticeStateSeriesOrder
import com.reinsteinquizbowl.order.api.ApiCompilation
import com.reinsteinquizbowl.order.api.ApiInvoiceLine
import com.reinsteinquizbowl.order.api.ApiNonConferenceGame
import com.reinsteinquizbowl.order.api.ApiPacket
import com.reinsteinquizbowl.order.api.ApiPacketExposure
import com.reinsteinquizbowl.order.api.ApiSchool
import com.reinsteinquizbowl.order.api.ApiStateSeries
import com.reinsteinquizbowl.order.api.ApiYear
import com.reinsteinquizbowl.order.entity.Booking
import com.reinsteinquizbowl.order.entity.BookingConference
import com.reinsteinquizbowl.order.entity.BookingConferencePacket
import com.reinsteinquizbowl.order.entity.BookingPracticeCompilationOrder
import com.reinsteinquizbowl.order.entity.BookingPracticePacketOrder
import com.reinsteinquizbowl.order.entity.BookingPracticeStateSeriesOrder
import com.reinsteinquizbowl.order.entity.Compilation
import com.reinsteinquizbowl.order.entity.InvoiceLine
import com.reinsteinquizbowl.order.entity.NonConferenceGame
import com.reinsteinquizbowl.order.entity.Packet
import com.reinsteinquizbowl.order.entity.PacketExposure
import com.reinsteinquizbowl.order.entity.School
import com.reinsteinquizbowl.order.entity.StateSeries
import com.reinsteinquizbowl.order.entity.Year
import com.reinsteinquizbowl.order.repository.BookingConferencePacketRepository
import com.reinsteinquizbowl.order.repository.BookingConferenceRepository
import com.reinsteinquizbowl.order.repository.BookingConferenceSchoolRepository
import com.reinsteinquizbowl.order.repository.BookingPracticeCompilationOrderRepository
import com.reinsteinquizbowl.order.repository.BookingPracticePacketOrderRepository
import com.reinsteinquizbowl.order.repository.BookingPracticeStateSeriesOrderRepository
import com.reinsteinquizbowl.order.repository.InvoiceLineRepository
import com.reinsteinquizbowl.order.repository.NonConferenceGameRepository
import com.reinsteinquizbowl.order.repository.NonConferenceGameSchoolRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Suppress("TooManyFunctions")
class Converter {
    @Autowired private lateinit var bookingConferenceRepo: BookingConferenceRepository
    @Autowired private lateinit var invoiceCalculator: InvoiceCalculator
    @Autowired private lateinit var bookingConferencePacketRepository: BookingConferencePacketRepository
    @Autowired private lateinit var bookingConferenceSchoolRepo: BookingConferenceSchoolRepository
    @Autowired private lateinit var bookingPracticeStateSeriesOrderRepo: BookingPracticeStateSeriesOrderRepository
    @Autowired private lateinit var bookingPracticePacketOrderRepo: BookingPracticePacketOrderRepository
    @Autowired private lateinit var bookingPracticeCompilationOrderRepo: BookingPracticeCompilationOrderRepository
    @Autowired private lateinit var invoiceLineRepo: InvoiceLineRepository
    @Autowired private lateinit var nonConferenceGameRepo: NonConferenceGameRepository
    @Autowired private lateinit var nonConferenceGameSchoolRepo: NonConferenceGameSchoolRepository

    fun toApi(entity: Booking): ApiBooking {
        val invoiceLines = invoiceLineRepo.findByBookingId(entity.id!!)

        return ApiBooking(
            id = entity.id,
            school = entity.school?.let { toApi(it) },
            name = entity.name,
            emailAddress = entity.emailAddress,
            authority = entity.authority,
            invoiceLabel = if (invoiceLines.isEmpty()) null else invoiceCalculator.determineInvoiceLabel(entity),
            cost = if (invoiceLines.isEmpty()) null else invoiceLines.sumOf(InvoiceLine::getTotalCost),
            statusCode = entity.status?.code,
            shipDate = entity.shipDate,
            paymentReceivedDate = entity.paymentReceivedDate,
            requestsW9 = entity.requestsW9,
            externalNote = entity.externalNote,
            internalNote = entity.internalNote, // THINK: how can we limit exposure to this?
            conference = bookingConferenceRepo.findByBookingId(entity.id!!)?.let { toApi(it) },
            nonConferenceGames = nonConferenceGameRepo.findByBookingId(entity.id!!).map { toApi(it) },
            stateSeriesOrders = bookingPracticeStateSeriesOrderRepo.findByBookingId(entity.id!!).map { toApi(it) },
            packetOrders = bookingPracticePacketOrderRepo.findByBookingId(entity.id!!).map { toApi(it) },
            compilationOrders = bookingPracticeCompilationOrderRepo.findByBookingId(entity.id!!).map { toApi(it) },
            invoiceLines = invoiceLines.sortedBy { it.sequence }.map { toApi(it) },
            createdAt = entity.createdAt,
            creationId = entity.creationId,
            modifiedAt = entity.modifiedAt,
        )
    }

    fun toApi(entity: BookingConference) = ApiBookingConference(
        id = entity.id,
        bookingId = entity.booking?.id,
        name = entity.name,
        packetsRequested = entity.packetsRequested,
        schoolIds = bookingConferenceSchoolRepo.findSchoolIdsByBookingConferenceId(entity.id!!),
        assignedPackets = bookingConferencePacketRepository.findByBookingConferenceId(entity.id!!)
            .mapNotNull(BookingConferencePacket::assignedPacket)
            .map { toApi(it) },
    )

    fun toApi(entity: BookingPracticeStateSeriesOrder) = ApiBookingPracticeStateSeriesOrder(
        id = entity.id,
        bookingId = entity.bookingId,
        stateSeries = entity.stateSeries?.let { toApi(it) },
    )

    fun toApi(entity: BookingPracticeCompilationOrder) = ApiBookingPracticeCompilationOrder(
        id = entity.id,
        bookingId = entity.bookingId,
        compilation = entity.compilation?.let { toApi(it) },
    )

    fun toApi(entity: BookingPracticePacketOrder) = ApiBookingPracticePacketOrder(
        id = entity.id,
        bookingId = entity.bookingId,
        packet = entity.packet?.let { toApi(it) },
    )

    fun toApi(entity: StateSeries) = ApiStateSeries(
        id = entity.id!!,
        name = entity.name!!,
        description = entity.description,
        price = entity.price!!,
        available = entity.available!!,
        sequence = entity.sequence!!,
    )

    fun toApi(entity: Compilation) = ApiCompilation(
        id = entity.id!!,
        name = entity.name!!,
        description = entity.description,
        price = entity.price!!,
        available = entity.available!!,
        sequence = entity.sequence!!,
    )

    fun toApi(entity: InvoiceLine) = ApiInvoiceLine(
        id = entity.id,
        bookingId = entity.bookingId,
        itemType = entity.itemType,
        itemId = entity.itemId,
        label = entity.label,
        quantity = entity.quantity,
        unitCost = entity.unitCost,
        sequence = entity.sequence,
    )

    fun toApi(entity: NonConferenceGame) = ApiNonConferenceGame(
        id = entity.id,
        bookingId = entity.bookingId,
        schoolIds = nonConferenceGameSchoolRepo.findSchoolIdsByNonConferenceGameId(entity.id!!),
        assignedPacket = entity.assignedPacket?.let { toApi(it) },
    )

    fun toApi(entity: Packet) = ApiPacket(
        id = entity.id!!,
        yearCode = entity.yearCode!!,
        number = entity.number!!,
        availableForCompetition = entity.availableForCompetition!!,
        priceAsPracticeMaterial = entity.priceAsPracticeMaterial,
    )

    fun toApi(entity: School) = ApiSchool(
        id = entity.id!!,
        name = entity.name!!,
        shortName = entity.shortName!!,
        address = entity.address!!,
        city = entity.city!!,
        state = entity.state!!,
        postalCode = entity.postalCode!!,
        country = entity.country!!,
        latitude = entity.latitude,
        longitude = entity.longitude,
        active = entity.active!!,
        coop = entity.coop!!,
        iesaId = entity.iesaId,
        note = entity.note,
    )

    fun toApi(entity: Year) = ApiYear(
        code = entity.code!!,
        name = entity.name!!,
        startDate = entity.startDate!!,
        endDate = entity.endDate!!,
        questionsShipStarting = entity.questionsShipStarting!!,
        maximumPacketPracticeMaterialPrice = entity.maximumPacketPracticeMaterialPrice,
    )

    fun toApi(entity: PacketExposure) = ApiPacketExposure(
        packetId = entity.packetId,
        exposedSchoolId = entity.exposedSchoolId,
        source = entity.source,
        sourceId = entity.sourceId,
        bookingId = entity.bookingId,
        bookingCreationId = entity.bookingCreationId,
        ordererSchoolId = entity.ordererSchoolId,
        tentativePacketExposure = entity.tentativePacketExposure,
        confirmedPacketExposure = entity.confirmedPacketExposure,
    )
}
