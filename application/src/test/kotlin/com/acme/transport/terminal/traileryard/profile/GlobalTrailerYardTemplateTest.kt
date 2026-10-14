package com.acme.transport.terminal.traileryard.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTrailerYardTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalTrailerYardTemplate("code-347", "title-160")
        assertEquals(GlobalTrailerYardTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newGlobalTrailerYardTemplate("code-347", "title-160")
        try {
            item.moveTo(GlobalTrailerYardTemplateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTrailerYardTemplateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalTrailerYardTemplate("code-347", "title-160")
        item.status = GlobalTrailerYardTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalTrailerYardTemplateStatus.CREATED) }
    }
}
