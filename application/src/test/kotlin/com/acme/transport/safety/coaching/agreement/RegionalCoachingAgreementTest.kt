package com.acme.transport.safety.coaching.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCoachingAgreementTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalCoachingAgreement("code-742", "label-274")
        assertEquals(RegionalCoachingAgreementStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRegionalCoachingAgreement("code-742", "label-274")
        try {
            item.moveTo(RegionalCoachingAgreementStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCoachingAgreementStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalCoachingAgreement("code-742", "label-274")
        item.status = RegionalCoachingAgreementStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalCoachingAgreementStatus.OPEN) }
    }
}
