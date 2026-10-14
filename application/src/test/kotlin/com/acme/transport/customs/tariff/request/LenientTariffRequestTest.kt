package com.acme.transport.customs.tariff.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientTariffRequestTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientTariffRequest("code-478", "name-645")
        assertEquals(LenientTariffRequestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientTariffRequest("code-478", "name-645")
        try {
            item.moveTo(LenientTariffRequestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientTariffRequestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientTariffRequest("code-478", "name-645")
        item.status = LenientTariffRequestStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientTariffRequestStatus.PLANNED) }
    }
}
