package com.acme.transport.network.cutoff.regionalcutoffsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newCutoffTemplate("code-638", "label-457")
        assertEquals(CutoffTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCutoffTemplate("code-638", "label-457")
        try {
            item.moveTo(CutoffTemplateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffTemplateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCutoffTemplate("code-638", "label-457")
        item.status = CutoffTemplateStatus.FAILED
        assertFails("move") { item.moveTo(CutoffTemplateStatus.NEW) }
    }
}
