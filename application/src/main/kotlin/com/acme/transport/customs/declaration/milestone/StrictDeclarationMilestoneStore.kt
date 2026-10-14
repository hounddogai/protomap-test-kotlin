package com.acme.transport.customs.declaration.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict declaration milestones by code. */
interface StrictDeclarationMilestoneStore {
    fun find(code: String): StrictDeclarationMilestone?
    fun save(item: StrictDeclarationMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDeclarationMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDeclarationMilestoneStore(
    var items: MutableMap<String, StrictDeclarationMilestone>,
    var order: MutableList<String>,
) : StrictDeclarationMilestoneStore {
    override fun find(code: String): StrictDeclarationMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDeclarationMilestone) {
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

    override fun list(): MutableList<StrictDeclarationMilestone> {
        val out = mutableListOf<StrictDeclarationMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDeclarationMilestoneStore(): StrictDeclarationMilestoneStore {
    return MemoryStrictDeclarationMilestoneStore(
        mutableMapOf<String, StrictDeclarationMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDeclarationMilestoneStore(
    var inner: StrictDeclarationMilestoneStore,
    var cache: TtlCache<StrictDeclarationMilestone>,
    var hits: Int,
    var misses: Int,
) : StrictDeclarationMilestoneStore {
    override fun find(code: String): StrictDeclarationMilestone? {
        val hit = (cache.get(code) as StrictDeclarationMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDeclarationMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDeclarationMilestone> = inner.list()
}

fun newCachedStrictDeclarationMilestoneStore(inner: StrictDeclarationMilestoneStore, ttl: Int, clock: Clock): StrictDeclarationMilestoneStore {
    return CachedStrictDeclarationMilestoneStore(
        inner = inner,
        cache = newTtlCache<StrictDeclarationMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict declaration milestones. Empty fields match all. */
data class StrictDeclarationMilestoneQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDeclarationMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDeclarationMilestone>): MutableList<StrictDeclarationMilestone> {
        val matched = filter(items) { candidate: StrictDeclarationMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDeclarationMilestones(store: StrictDeclarationMilestoneStore, query: StrictDeclarationMilestoneQuery): MutableList<StrictDeclarationMilestone> {
    return query.apply(store.list())
}
