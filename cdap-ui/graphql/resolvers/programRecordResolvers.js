const programsResolver = {
  ApplicationDetail: {
    programs: async (parent, args, context, info) => {
      return await (new Promise((resolve, reject) => {
        const programs = parent.programs
        const type = args.type

        if (type == null) {
          resolve(programs)
        }
        else {
          typePrograms = programs.filter(
            function (program) {
              return program.type == type
            }
          );
          resolve(typePrograms);
        }
      }));
    }
  }
}

const programRecordResolvers = programsResolver

module.exports = {
  programRecordResolvers
}