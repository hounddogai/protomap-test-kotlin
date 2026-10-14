package com.acme.transport.terminal.sortation.sortationbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalSortationOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalSortationOverride("code-825", "name-14")
        assertEquals(RegionalSortationOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRegionalSortationOverride("code-825", "name-14")
        try {
            item.moveTo(RegionalSortationOverrideStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalSortationOverrideStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalSortationOverride("code-825", "name-14")
        item.status = RegionalSortationOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalSortationOverrideStatus.CREATED) }
    }
}
