package com.acme.transport.network.transittime.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredTransitTimeNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredTransitTimeNoticeEngine(newRegistry())
        val item = newTieredTransitTimeNotice("code-560", "title-790")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
