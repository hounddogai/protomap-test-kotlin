package com.acme.transport.safety.violation.primaryviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultViolationRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultViolationRollupStore()
        val item = newDefaultViolationRollup("code-609", "label-125")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
