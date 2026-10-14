package com.acme.transport.customs.declaration.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDeclarationPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newLenientDeclarationPolicy("code-280", "name-209")
        assertEquals(LenientDeclarationPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLenientDeclarationPolicy("code-280", "name-209")
        try {
            item.moveTo(LenientDeclarationPolicyStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDeclarationPolicyStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientDeclarationPolicy("code-280", "name-209")
        item.status = LenientDeclarationPolicyStatus.FAILED
        assertFails("move") { item.moveTo(LenientDeclarationPolicyStatus.NEW) }
    }
}
