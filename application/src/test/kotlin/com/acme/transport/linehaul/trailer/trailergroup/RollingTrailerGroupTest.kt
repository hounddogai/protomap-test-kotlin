package com.acme.transport.linehaul.trailer.trailergroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTrailerGroupTest {
    @Test
    fun `starts in created`() {
        val item = newRollingTrailerGroup("code-840", "label-303")
        assertEquals(RollingTrailerGroupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRollingTrailerGroup("code-840", "label-303")
        try {
            item.moveTo(RollingTrailerGroupStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTrailerGroupStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingTrailerGroup("code-840", "label-303")
        item.status = RollingTrailerGroupStatus.RETIRED
        assertFails("move") { item.moveTo(RollingTrailerGroupStatus.CREATED) }
    }
}
