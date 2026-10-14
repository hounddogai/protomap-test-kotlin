package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultRedeliveryNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultRedeliveryNoticeEngine(newRegistry())
        val item = newDefaultRedeliveryNotice("code-902", "name-227")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
