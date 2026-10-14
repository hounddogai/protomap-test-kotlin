package com.acme.transport.crewing.roster.rosterquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRosterRollupTest {
    @Test
    fun `starts in created`() {
        val item = newRollingRosterRollup("code-793", "label-274")
        assertEquals(RollingRosterRollupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRollingRosterRollup("code-793", "label-274")
        try {
            item.moveTo(RollingRosterRollupStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRosterRollupStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingRosterRollup("code-793", "label-274")
        item.status = RollingRosterRollupStatus.RETIRED
        assertFails("move") { item.moveTo(RollingRosterRollupStatus.CREATED) }
    }
}
