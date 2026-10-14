package com.acme.transport.customs.tariff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTariffBatchTest {
    @Test
    fun `starts in open`() {
        val item = newRollingTariffBatch("code-607", "title-608")
        assertEquals(RollingTariffBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRollingTariffBatch("code-607", "title-608")
        try {
            item.moveTo(RollingTariffBatchStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTariffBatchStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingTariffBatch("code-607", "title-608")
        item.status = RollingTariffBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingTariffBatchStatus.OPEN) }
    }
}
