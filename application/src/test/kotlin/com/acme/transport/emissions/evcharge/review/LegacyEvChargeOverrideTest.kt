package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyEvChargeOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyEvChargeOverride("code-74", "title-887")
        assertEquals(LegacyEvChargeOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newLegacyEvChargeOverride("code-74", "title-887")
        try {
            item.moveTo(LegacyEvChargeOverrideStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyEvChargeOverrideStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyEvChargeOverride("code-74", "title-887")
        item.status = LegacyEvChargeOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyEvChargeOverrideStatus.OPEN) }
    }
}
