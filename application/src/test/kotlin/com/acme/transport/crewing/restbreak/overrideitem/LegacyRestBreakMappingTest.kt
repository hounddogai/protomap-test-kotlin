package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRestBreakMappingTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyRestBreakMapping("code-834", "title-469")
        assertEquals(LegacyRestBreakMappingStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLegacyRestBreakMapping("code-834", "title-469")
        try {
            item.moveTo(LegacyRestBreakMappingStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRestBreakMappingStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyRestBreakMapping("code-834", "title-469")
        item.status = LegacyRestBreakMappingStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyRestBreakMappingStatus.PROPOSED) }
    }
}
