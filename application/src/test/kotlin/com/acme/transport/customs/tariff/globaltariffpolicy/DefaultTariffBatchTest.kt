package com.acme.transport.customs.tariff.globaltariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTariffBatchTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultTariffBatch("code-833", "title-744")
        assertEquals(DefaultTariffBatchStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultTariffBatch("code-833", "title-744")
        try {
            item.moveTo(DefaultTariffBatchStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTariffBatchStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultTariffBatch("code-833", "title-744")
        item.status = DefaultTariffBatchStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultTariffBatchStatus.CREATED) }
    }
}
