package com.acme.transport.safety.incident.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalIncidentAgreementTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalIncidentAgreement("code-640", "title-894")
        assertEquals(RegionalIncidentAgreementStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRegionalIncidentAgreement("code-640", "title-894")
        try {
            item.moveTo(RegionalIncidentAgreementStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalIncidentAgreementStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalIncidentAgreement("code-640", "title-894")
        item.status = RegionalIncidentAgreementStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalIncidentAgreementStatus.OPEN) }
    }
}
