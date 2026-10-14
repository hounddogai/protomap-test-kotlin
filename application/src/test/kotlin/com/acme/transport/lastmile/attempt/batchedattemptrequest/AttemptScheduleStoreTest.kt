package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptScheduleStore()
        val item = newAttemptSchedule("code-633", "name-498")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
