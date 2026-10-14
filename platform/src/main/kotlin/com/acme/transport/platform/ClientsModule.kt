package com.acme.transport.platform

import com.acme.transport.platform.clients.admissionsClients
import com.acme.transport.platform.clients.appointmentsClients
import com.acme.transport.platform.clients.assetsClients
import com.acme.transport.platform.clients.audiencesClients
import com.acme.transport.platform.clients.auditClients
import com.acme.transport.platform.clients.authorizationClients
import com.acme.transport.platform.clients.billingClients
import com.acme.transport.platform.clients.boxofficeClients
import com.acme.transport.platform.clients.broadcastsClients
import com.acme.transport.platform.clients.budgetingClients
import com.acme.transport.platform.clients.careplansClients
import com.acme.transport.platform.clients.cartClients
import com.acme.transport.platform.clients.catalogClients
import com.acme.transport.platform.clients.certificatesClients
import com.acme.transport.platform.clients.chatClients
import com.acme.transport.platform.clients.checkoutClients
import com.acme.transport.platform.clients.communityClients
import com.acme.transport.platform.clients.complianceClients
import com.acme.transport.platform.clients.concessionsClients
import com.acme.transport.platform.clients.configurationClients
import com.acme.transport.platform.clients.contractsClients
import com.acme.transport.platform.clients.corporateeventsClients
import com.acme.transport.platform.clients.customersClients
import com.acme.transport.platform.clients.datasetsClients
import com.acme.transport.platform.clients.deliveriesClients
import com.acme.transport.platform.clients.devicesClients
import com.acme.transport.platform.clients.dispatchClients
import com.acme.transport.platform.clients.documentsClients
import com.acme.transport.platform.clients.driverappClients
import com.acme.transport.platform.clients.driversClients
import com.acme.transport.platform.clients.edgegatewayClients
import com.acme.transport.platform.clients.expensesClients
import com.acme.transport.platform.clients.fanportalClients
import com.acme.transport.platform.clients.featureflagsClients
import com.acme.transport.platform.clients.featurestoreClients
import com.acme.transport.platform.clients.fleetClients
import com.acme.transport.platform.clients.fuelClients
import com.acme.transport.platform.clients.identityClients
import com.acme.transport.platform.clients.insuranceClients
import com.acme.transport.platform.clients.inventoryClients
import com.acme.transport.platform.clients.itsmClients
import com.acme.transport.platform.clients.keymanagementClients
import com.acme.transport.platform.clients.learningClients
import com.acme.transport.platform.clients.ledgerClients
import com.acme.transport.platform.clients.lendingClients
import com.acme.transport.platform.clients.licensingClients
import com.acme.transport.platform.clients.lineageClients
import com.acme.transport.platform.clients.localizationClients
import com.acme.transport.platform.clients.maintenanceClients
import com.acme.transport.platform.clients.mediaClients
import com.acme.transport.platform.clients.metricsClients
import com.acme.transport.platform.clients.modelregistryClients
import com.acme.transport.platform.clients.notificationsClients
import com.acme.transport.platform.clients.onboardingClients
import com.acme.transport.platform.clients.ordersClients
import com.acme.transport.platform.clients.patientappClients
import com.acme.transport.platform.clients.patientsClients
import com.acme.transport.platform.clients.payrollClients
import com.acme.transport.platform.clients.performanceClients
import com.acme.transport.platform.clients.performersClients
import com.acme.transport.platform.clients.personalizationClients
import com.acme.transport.platform.clients.pipelinesClients
import com.acme.transport.platform.clients.prescriptionsClients
import com.acme.transport.platform.clients.ratelimitingClients
import com.acme.transport.platform.clients.recruitingClients
import com.acme.transport.platform.clients.returnsClients
import com.acme.transport.platform.clients.riskClients
import com.acme.transport.platform.clients.routingClients
import com.acme.transport.platform.clients.searchClients
import com.acme.transport.platform.clients.seatingClients
import com.acme.transport.platform.clients.secretsClients
import com.acme.transport.platform.clients.shippingClients
import com.acme.transport.platform.clients.showsClients
import com.acme.transport.platform.clients.sponsorshipsClients
import com.acme.transport.platform.clients.subscriptionsClients
import com.acme.transport.platform.clients.suppliersClients
import com.acme.transport.platform.clients.surveysClients
import com.acme.transport.platform.clients.telehealthClients
import com.acme.transport.platform.clients.telematicsClients
import com.acme.transport.platform.clients.ticketingClients
import com.acme.transport.platform.clients.toursClients
import com.acme.transport.platform.clients.travelClients
import com.acme.transport.platform.clients.treasuryClients
import com.acme.transport.platform.clients.venuesClients
import com.acme.transport.platform.clients.warehousingClients
import com.acme.transport.platform.clients.webhooksClients
import com.acme.transport.platform.clients.workplacesClients
import com.acme.transport.platform.clients.yardsClients
import org.koin.dsl.module

/** Import each API's client module without making one oversized JVM initializer. */
val clientsModule = module {
    includes(
        admissionsClients,
        appointmentsClients,
        assetsClients,
        audiencesClients,
        auditClients,
        authorizationClients,
        billingClients,
        boxofficeClients,
        broadcastsClients,
        budgetingClients,
        careplansClients,
        cartClients,
        catalogClients,
        certificatesClients,
        chatClients,
        checkoutClients,
        communityClients,
        complianceClients,
        concessionsClients,
        configurationClients,
        contractsClients,
        corporateeventsClients,
        customersClients,
        datasetsClients,
        deliveriesClients,
        devicesClients,
        dispatchClients,
        documentsClients,
        driverappClients,
        driversClients,
        edgegatewayClients,
        expensesClients,
        fanportalClients,
        featureflagsClients,
        featurestoreClients,
        fleetClients,
        fuelClients,
        identityClients,
        insuranceClients,
        inventoryClients,
        itsmClients,
        keymanagementClients,
        learningClients,
        ledgerClients,
        lendingClients,
        licensingClients,
        lineageClients,
        localizationClients,
        maintenanceClients,
        mediaClients,
        metricsClients,
        modelregistryClients,
        notificationsClients,
        onboardingClients,
        ordersClients,
        patientappClients,
        patientsClients,
        payrollClients,
        performanceClients,
        performersClients,
        personalizationClients,
        pipelinesClients,
        prescriptionsClients,
        ratelimitingClients,
        recruitingClients,
        returnsClients,
        riskClients,
        routingClients,
        searchClients,
        seatingClients,
        secretsClients,
        shippingClients,
        showsClients,
        sponsorshipsClients,
        subscriptionsClients,
        suppliersClients,
        surveysClients,
        telehealthClients,
        telematicsClients,
        ticketingClients,
        toursClients,
        travelClients,
        treasuryClients,
        venuesClients,
        warehousingClients,
        webhooksClients,
        workplacesClients,
        yardsClients,
    )
}
