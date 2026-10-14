package com.acme.transport.emissions.evcharge.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeMappingTest {
    @Test
    fun `starts in open`() {
        val item = newEvChargeMapping("code-566", "name-912")
        assertEquals(EvChargeMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newEvChargeMapping("code-566", "name-912")
        try {
            item.moveTo(EvChargeMappingStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeMappingStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newEvChargeMapping("code-566", "name-912")
        item.status = EvChargeMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(EvChargeMappingStatus.OPEN) }
    }
}
