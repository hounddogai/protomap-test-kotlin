package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedEvChargePreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedEvChargePreference("code-401", "label-450")
        assertEquals(CappedEvChargePreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newCappedEvChargePreference("code-401", "label-450")
        try {
            item.moveTo(CappedEvChargePreferenceStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedEvChargePreferenceStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedEvChargePreference("code-401", "label-450")
        item.status = CappedEvChargePreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedEvChargePreferenceStatus.REQUESTED) }
    }
}
