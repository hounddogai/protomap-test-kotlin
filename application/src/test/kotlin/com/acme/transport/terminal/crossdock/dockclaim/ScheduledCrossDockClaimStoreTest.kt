package com.acme.transport.terminal.crossdock.dockclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledCrossDockClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledCrossDockClaimStore()
        val item = newScheduledCrossDockClaim("code-721", "name-732")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
