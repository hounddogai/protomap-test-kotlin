package com.acme.transport.customs.tariff.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTariffRequestTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultTariffRequest("code-890", "name-26")
        assertEquals(DefaultTariffRequestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDefaultTariffRequest("code-890", "name-26")
        try {
            item.moveTo(DefaultTariffRequestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTariffRequestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultTariffRequest("code-890", "name-26")
        item.status = DefaultTariffRequestStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultTariffRequestStatus.PLANNED) }
    }
}
