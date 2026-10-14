package com.acme.transport.customs.declaration.notice

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled declaration baselines by code. */
interface ScheduledDeclarationBaselineStore {
    fun find(code: String): ScheduledDeclarationBaseline?
    fun save(item: ScheduledDeclarationBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDeclarationBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDeclarationBaselineStore(
    var items: MutableMap<String, ScheduledDeclarationBaseline>,
    var order: MutableList<String>,
) : ScheduledDeclarationBaselineStore {
    override fun find(code: String): ScheduledDeclarationBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDeclarationBaseline) {
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

    override fun list(): MutableList<ScheduledDeclarationBaseline> {
        val out = mutableListOf<ScheduledDeclarationBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDeclarationBaselineStore(): ScheduledDeclarationBaselineStore {
    return MemoryScheduledDeclarationBaselineStore(
        mutableMapOf<String, ScheduledDeclarationBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDeclarationBaselineStore(
    var inner: ScheduledDeclarationBaselineStore,
    var cache: TtlCache<ScheduledDeclarationBaseline>,
    var hits: Int,
    var misses: Int,
) : ScheduledDeclarationBaselineStore {
    override fun find(code: String): ScheduledDeclarationBaseline? {
        val hit = (cache.get(code) as ScheduledDeclarationBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDeclarationBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDeclarationBaseline> = inner.list()
}

fun newCachedScheduledDeclarationBaselineStore(inner: ScheduledDeclarationBaselineStore, ttl: Int, clock: Clock): ScheduledDeclarationBaselineStore {
    return CachedScheduledDeclarationBaselineStore(
        inner = inner,
        cache = newTtlCache<ScheduledDeclarationBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled declaration baselines. Empty fields match all. */
data class ScheduledDeclarationBaselineQuery(
    var category: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDeclarationBaseline): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDeclarationBaseline>): MutableList<ScheduledDeclarationBaseline> {
        val matched = filter(items) { candidate: ScheduledDeclarationBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDeclarationBaselines(store: ScheduledDeclarationBaselineStore, query: ScheduledDeclarationBaselineQuery): MutableList<ScheduledDeclarationBaseline> {
    return query.apply(store.list())
}
