package com.acme.transport.linehaul.trailer.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTrailerTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredTrailerTemplate("code-35", "label-144")
        assertEquals(TieredTrailerTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTieredTrailerTemplate("code-35", "label-144")
        try {
            item.moveTo(TieredTrailerTemplateStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTrailerTemplateStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredTrailerTemplate("code-35", "label-144")
        item.status = TieredTrailerTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredTrailerTemplateStatus.PLANNED) }
    }
}
