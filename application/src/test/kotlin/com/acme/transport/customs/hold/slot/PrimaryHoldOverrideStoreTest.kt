package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryHoldOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryHoldOverrideStore()
        val item = newPrimaryHoldOverride("code-784", "title-381")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
