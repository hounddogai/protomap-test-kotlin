package com.acme.transport.customs.tariff.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newTariffPolicy("code-142", "title-882")
        assertEquals(TariffPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTariffPolicy("code-142", "title-882")
        try {
            item.moveTo(TariffPolicyStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffPolicyStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTariffPolicy("code-142", "title-882")
        item.status = TariffPolicyStatus.FAILED
        assertFails("move") { item.moveTo(TariffPolicyStatus.NEW) }
    }
}
