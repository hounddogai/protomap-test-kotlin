package com.acme.transport.terminal.dwell.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDwellProfileTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalDwellProfile("code-99", "title-267")
        assertEquals(RegionalDwellProfileStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRegionalDwellProfile("code-99", "title-267")
        try {
            item.moveTo(RegionalDwellProfileStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDwellProfileStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalDwellProfile("code-99", "title-267")
        item.status = RegionalDwellProfileStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalDwellProfileStatus.OPEN) }
    }
}
