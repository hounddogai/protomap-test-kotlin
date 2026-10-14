package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDeclarationRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingDeclarationRecord("code-3", "title-961")
        assertEquals(RollingDeclarationRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRollingDeclarationRecord("code-3", "title-961")
        try {
            item.moveTo(RollingDeclarationRecordStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDeclarationRecordStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingDeclarationRecord("code-3", "title-961")
        item.status = RollingDeclarationRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingDeclarationRecordStatus.REQUESTED) }
    }
}
