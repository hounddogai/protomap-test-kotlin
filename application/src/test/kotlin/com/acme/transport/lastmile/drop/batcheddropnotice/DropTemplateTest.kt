package com.acme.transport.lastmile.drop.batcheddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropTemplateTest {
    @Test
    fun `starts in idle`() {
        val item = newDropTemplate("code-958", "label-878")
        assertEquals(DropTemplateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDropTemplate("code-958", "label-878")
        try {
            item.moveTo(DropTemplateStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropTemplateStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDropTemplate("code-958", "label-878")
        item.status = DropTemplateStatus.STOPPED
        assertFails("move") { item.moveTo(DropTemplateStatus.IDLE) }
    }
}
