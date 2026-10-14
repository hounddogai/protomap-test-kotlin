package com.acme.transport.safety.violation.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingViolationProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingViolationProfileStore()
        val item = newRollingViolationProfile("code-722", "title-720")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
