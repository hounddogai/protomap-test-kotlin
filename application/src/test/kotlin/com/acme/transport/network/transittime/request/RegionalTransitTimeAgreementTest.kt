package com.acme.transport.network.transittime.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalTransitTimeAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalTransitTimeAgreement("code-508", "name-752")
        assertEquals(RegionalTransitTimeAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRegionalTransitTimeAgreement("code-508", "name-752")
        try {
            item.moveTo(RegionalTransitTimeAgreementStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalTransitTimeAgreementStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalTransitTimeAgreement("code-508", "name-752")
        item.status = RegionalTransitTimeAgreementStatus.FAILED
        assertFails("move") { item.moveTo(RegionalTransitTimeAgreementStatus.NEW) }
    }
}
