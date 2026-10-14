package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HoldNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldNoticeEngine(newRegistry())
        val item = newHoldNotice("code-126", "title-940")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
