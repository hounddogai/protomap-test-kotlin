package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictBackhaulOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newStrictBackhaulOverride("code-249", "title-902")
        assertEquals(StrictBackhaulOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newStrictBackhaulOverride("code-249", "title-902")
        try {
            item.moveTo(StrictBackhaulOverrideStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictBackhaulOverrideStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictBackhaulOverride("code-249", "title-902")
        item.status = StrictBackhaulOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictBackhaulOverrideStatus.OPEN) }
    }
}
