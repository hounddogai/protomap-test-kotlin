package com.acme.transport.linehaul.backhaul.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalBackhaulBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalBackhaulBaseline("code-682", "title-135")
        assertEquals(RegionalBackhaulBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRegionalBackhaulBaseline("code-682", "title-135")
        try {
            item.moveTo(RegionalBackhaulBaselineStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalBackhaulBaselineStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalBackhaulBaseline("code-682", "title-135")
        item.status = RegionalBackhaulBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalBackhaulBaselineStatus.OPEN) }
    }
}
