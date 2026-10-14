package com.acme.transport.customs.declaration.baseline

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration milestones by code. */
interface DeclarationMilestoneStore {
    fun find(code: String): DeclarationMilestone?
    fun save(item: DeclarationMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationMilestoneStore(
    var items: MutableMap<String, DeclarationMilestone>,
    var order: MutableList<String>,
) : DeclarationMilestoneStore {
    override fun find(code: String): DeclarationMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationMilestone) {
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

    override fun list(): MutableList<DeclarationMilestone> {
        val out = mutableListOf<DeclarationMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationMilestoneStore(): DeclarationMilestoneStore {
    return MemoryDeclarationMilestoneStore(
        mutableMapOf<String, DeclarationMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationMilestoneStore(
    var inner: DeclarationMilestoneStore,
    var cache: TtlCache<DeclarationMilestone>,
    var hits: Int,
    var misses: Int,
) : DeclarationMilestoneStore {
    override fun find(code: String): DeclarationMilestone? {
        val hit = (cache.get(code) as DeclarationMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationMilestone> = inner.list()
}

fun newCachedDeclarationMilestoneStore(inner: DeclarationMilestoneStore, ttl: Int, clock: Clock): DeclarationMilestoneStore {
    return CachedDeclarationMilestoneStore(
        inner = inner,
        cache = newTtlCache<DeclarationMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration milestones. Empty fields match all. */
data class DeclarationMilestoneQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationMilestone): Boolean {
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

    fun apply(items: MutableList<DeclarationMilestone>): MutableList<DeclarationMilestone> {
        val matched = filter(items) { candidate: DeclarationMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationMilestones(store: DeclarationMilestoneStore, query: DeclarationMilestoneQuery): MutableList<DeclarationMilestone> {
    return query.apply(store.list())
}
