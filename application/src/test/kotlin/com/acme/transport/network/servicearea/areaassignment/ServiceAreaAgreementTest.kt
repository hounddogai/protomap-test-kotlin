package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaAgreementTest {
    @Test
    fun `starts in open`() {
        val item = newServiceAreaAgreement("code-746", "title-774")
        assertEquals(ServiceAreaAgreementStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newServiceAreaAgreement("code-746", "title-774")
        try {
            item.moveTo(ServiceAreaAgreementStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaAgreementStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newServiceAreaAgreement("code-746", "title-774")
        item.status = ServiceAreaAgreementStatus.ARCHIVED
        assertFails("move") { item.moveTo(ServiceAreaAgreementStatus.OPEN) }
    }
}
