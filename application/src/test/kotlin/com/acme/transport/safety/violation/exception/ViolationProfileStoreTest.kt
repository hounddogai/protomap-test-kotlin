package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationProfileStore()
        val item = newViolationProfile("code-625", "label-815")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
