db.getCollection('TC').aggregate([
    {
        $group: {
            _id: '$result.status',
            count: {$sum: 1}
        }
    }
])