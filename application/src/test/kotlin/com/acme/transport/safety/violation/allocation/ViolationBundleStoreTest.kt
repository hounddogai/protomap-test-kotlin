package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationBundleStore()
        val item = newViolationBundle("code-604", "name-521")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
