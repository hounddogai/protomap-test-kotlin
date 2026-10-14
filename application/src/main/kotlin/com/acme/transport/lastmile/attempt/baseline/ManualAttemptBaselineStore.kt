package com.acme.transport.lastmile.attempt.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual attempt baselines by code. */
interface ManualAttemptBaselineStore {
    fun find(code: String): ManualAttemptBaseline?
    fun save(item: ManualAttemptBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAttemptBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAttemptBaselineStore(
    var items: MutableMap<String, ManualAttemptBaseline>,
    var order: MutableList<String>,
) : ManualAttemptBaselineStore {
    override fun find(code: String): ManualAttemptBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAttemptBaseline) {
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

    override fun list(): MutableList<ManualAttemptBaseline> {
        val out = mutableListOf<ManualAttemptBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAttemptBaselineStore(): ManualAttemptBaselineStore {
    return MemoryManualAttemptBaselineStore(
        mutableMapOf<String, ManualAttemptBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAttemptBaselineStore(
    var inner: ManualAttemptBaselineStore,
    var cache: TtlCache<ManualAttemptBaseline>,
    var hits: Int,
    var misses: Int,
) : ManualAttemptBaselineStore {
    override fun find(code: String): ManualAttemptBaseline? {
        val hit = (cache.get(code) as ManualAttemptBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAttemptBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAttemptBaseline> = inner.list()
}

fun newCachedManualAttemptBaselineStore(inner: ManualAttemptBaselineStore, ttl: Int, clock: Clock): ManualAttemptBaselineStore {
    return CachedManualAttemptBaselineStore(
        inner = inner,
        cache = newTtlCache<ManualAttemptBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual attempt baselines. Empty fields match all. */
data class ManualAttemptBaselineQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAttemptBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualAttemptBaseline>): MutableList<ManualAttemptBaseline> {
        val matched = filter(items) { candidate: ManualAttemptBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAttemptBaselines(store: ManualAttemptBaselineStore, query: ManualAttemptBaselineQuery): MutableList<ManualAttemptBaseline> {
    return query.apply(store.list())
}
