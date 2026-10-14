package com.acme.transport.customs.declaration.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDeclarationTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictDeclarationTarget("code-281", "label-551")
        assertEquals(StrictDeclarationTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newStrictDeclarationTarget("code-281", "label-551")
        try {
            item.moveTo(StrictDeclarationTargetStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDeclarationTargetStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictDeclarationTarget("code-281", "label-551")
        item.status = StrictDeclarationTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictDeclarationTargetStatus.REQUESTED) }
    }
}
