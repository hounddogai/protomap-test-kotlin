package com.acme.transport.lastmile.locker.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingLockerPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingLockerPolicyStore()
        val item = newRollingLockerPolicy("code-408", "title-959")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
