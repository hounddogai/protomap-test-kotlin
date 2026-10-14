package com.acme.transport.network.cutoff.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCutoffTemplateTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientCutoffTemplate("code-492", "title-113")
        assertEquals(LenientCutoffTemplateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLenientCutoffTemplate("code-492", "title-113")
        try {
            item.moveTo(LenientCutoffTemplateStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCutoffTemplateStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientCutoffTemplate("code-492", "title-113")
        item.status = LenientCutoffTemplateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientCutoffTemplateStatus.PROPOSED) }
    }
}
