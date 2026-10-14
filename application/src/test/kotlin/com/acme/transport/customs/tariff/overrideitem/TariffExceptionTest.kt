package com.acme.transport.customs.tariff.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newTariffException("code-258", "title-396")
        assertEquals(TariffExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTariffException("code-258", "title-396")
        try {
            item.moveTo(TariffExceptionStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffExceptionStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTariffException("code-258", "title-396")
        item.status = TariffExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(TariffExceptionStatus.IDLE) }
    }
}
