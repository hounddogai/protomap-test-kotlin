package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default accessorial scores by code. */
interface DefaultAccessorialScoreStore {
    fun find(code: String): DefaultAccessorialScore?
    fun save(item: DefaultAccessorialScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultAccessorialScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultAccessorialScoreStore(
    var items: MutableMap<String, DefaultAccessorialScore>,
    var order: MutableList<String>,
) : DefaultAccessorialScoreStore {
    override fun find(code: String): DefaultAccessorialScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultAccessorialScore) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<DefaultAccessorialScore> {
        val out = mutableListOf<DefaultAccessorialScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultAccessorialScoreStore(): DefaultAccessorialScoreStore {
    return MemoryDefaultAccessorialScoreStore(
        mutableMapOf<String, DefaultAccessorialScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultAccessorialScoreStore(
    var inner: DefaultAccessorialScoreStore,
    var cache: TtlCache<DefaultAccessorialScore>,
    var hits: Int,
    var misses: Int,
) : DefaultAccessorialScoreStore {
    override fun find(code: String): DefaultAccessorialScore? {
        val hit = (cache.get(code) as DefaultAccessorialScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultAccessorialScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultAccessorialScore> = inner.list()
}

fun newCachedDefaultAccessorialScoreStore(inner: DefaultAccessorialScoreStore, ttl: Int, clock: Clock): DefaultAccessorialScoreStore {
    return CachedDefaultAccessorialScoreStore(
        inner = inner,
        cache = newTtlCache<DefaultAccessorialScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default accessorial scores. Empty fields match all. */
data class DefaultAccessorialScoreQuery(
    var channel: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultAccessorialScore): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultAccessorialScore>): MutableList<DefaultAccessorialScore> {
        val matched = filter(items) { candidate: DefaultAccessorialScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultAccessorialScores(store: DefaultAccessorialScoreStore, query: DefaultAccessorialScoreQuery): MutableList<DefaultAccessorialScore> {
    return query.apply(store.list())
}
