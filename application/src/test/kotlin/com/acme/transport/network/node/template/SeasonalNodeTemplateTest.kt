package com.acme.transport.network.node.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalNodeTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalNodeTemplate("code-345", "label-881")
        assertEquals(SeasonalNodeTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newSeasonalNodeTemplate("code-345", "label-881")
        try {
            item.moveTo(SeasonalNodeTemplateStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalNodeTemplateStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalNodeTemplate("code-345", "label-881")
        item.status = SeasonalNodeTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalNodeTemplateStatus.DRAFT) }
    }
}
