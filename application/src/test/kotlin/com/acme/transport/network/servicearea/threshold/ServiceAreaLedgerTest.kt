package com.acme.transport.network.servicearea.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newServiceAreaLedger("code-960", "title-66")
        assertEquals(ServiceAreaLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newServiceAreaLedger("code-960", "title-66")
        try {
            item.moveTo(ServiceAreaLedgerStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaLedgerStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newServiceAreaLedger("code-960", "title-66")
        item.status = ServiceAreaLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(ServiceAreaLedgerStatus.CREATED) }
    }
}
